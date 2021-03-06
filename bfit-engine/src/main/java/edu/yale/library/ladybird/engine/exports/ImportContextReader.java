package edu.yale.library.ladybird.engine.exports;

import edu.yale.library.ladybird.engine.cron.queue.ImportContextQueue;
import edu.yale.library.ladybird.engine.imports.Import;
import edu.yale.library.ladybird.engine.imports.Import.Row;
import edu.yale.library.ladybird.engine.imports.ImportContext;
import edu.yale.library.ladybird.engine.imports.ImportValue;
import edu.yale.library.ladybird.engine.metadata.FieldConstantUtil;
import edu.yale.library.ladybird.engine.model.FunctionConstants;
import edu.yale.library.ladybird.entity.FieldConstant;
import edu.yale.library.ladybird.entity.ImportJobContents;
import edu.yale.library.ladybird.entity.ImportJobExhead;
import edu.yale.library.ladybird.persistence.dao.ImportJobContentsDAO;
import edu.yale.library.ladybird.persistence.dao.ImportJobExheadDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportJobContentsHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportJobExheadHibernateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prepares final metadata by reading from import job tables and merging with OAI data.
 * @see ImportContextReaderOaiMerger
 */
/**
 * @author Osman Din {@literal <osman.din.yale@gmail.com>}
 */
public class ImportContextReader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    final ImportJobContentsDAO importJobContentsDAO = new ImportJobContentsHibernateDAO();

    final ImportJobExheadDAO importJobExheadDAO = new ImportJobExheadHibernateDAO();

    /**
     * Main method. Reads import tables (import job contents and import source) and merges OAI data
     * @return ImportEntityContext
     */
    public ImportContext read() {
        final ExportRequestEvent exportRequestEvent = ImportContextQueue.getJob(); //from Queue

        if (exportRequestEvent == null) {
            return null;
        }

        final int importId = exportRequestEvent.getImportId();
        final int numRowsToWrite = importJobContentsDAO.getNumRowsPerImportJob(importId) + 1;

        logger.debug("Read job={} from export engine queue. Expected num rows to write={}", importId, numRowsToWrite);

        if (numRowsToWrite <= 1) {
            logger.debug("No rows to write for importId={}!", importId);
            ImportContext empty = ImportContext.newInstance();
            empty.setImportId(importId);
            return empty;
        }

        //Get all FieldConstant. Each should have a column in the output.
        final List<FieldConstant> ladybirdFieldConstants = FieldConstantUtil.getApplicationFieldConstants();

        //Write exhead
        final Row exheadRow = new Import().new Row();

        for (final FieldConstant fieldConst : ladybirdFieldConstants) {
            Import importEntity = new Import();
            exheadRow.getColumns().add(importEntity.new Column(fieldConst, fieldConst.getName()));
        }

        final List<Row> resultRowList = new ArrayList<>();
        resultRowList.add(exheadRow); //N.B. exhead row added

        //Get import job contents rows of columns. These will be MERGED with the oai data:
        final List<Row> plainRows = new ImportWriterConverter().read(importId);
        logger.debug("Import job contents rows size={} for importId={}", plainRows.size(), importId);

        logger.debug("Merging with OAI provider values for importId={}", importId);
        ImportContextReaderOaiMerger importContextReaderOaiMerger = new ImportContextReaderOaiMerger();
        int oaiColIndex = getLocalIdentifierColumnNum(plainRows);

        if (oaiColIndex == -1) {
            logger.debug("Col with f104/f105 data not found in this dataset(sheet) for importId={}", importId);
        }

        List<Row> contentRows = importContextReaderOaiMerger.merge(importId, oaiColIndex, ladybirdFieldConstants, plainRows);
        resultRowList.addAll(contentRows);

        final ImportContext iContext = new ImportContext();
        iContext.setImportRowsList(resultRowList);
        iContext.setJobRequest(exportRequestEvent.getJobRequest());
        iContext.setImportId(importId);
        return iContext;
    }


    /**
     * Gets F104/F105 position. Should probably be extracted?
     * @param regularRows List of Row
     * @return position
     */
    private int getLocalIdentifierColumnNum(List<Row> regularRows) {
        ImportValue importValue = new ImportValue(regularRows);

        int index = -1;
        try {
            index = importValue.getFunctionPosition(FunctionConstants.F104); //or F105 TODO
        } catch (Exception e) {
            //logger.debug("Col with bib data not found in this dataset(sheet)");
        }

        if (index == -1) {
            try {
                index = importValue.getFunctionPosition(FunctionConstants.F105); //or F105 TODO
            } catch (Exception e) {
                //logger.debug("Col with barcode data not found in this dataset(sheet)");
            }
        }
        return index;
    }

    /**
     * Utility to convert db values into List<Row>
     */
    private class ImportWriterConverter {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        /**
         * Returns rows from import job tables
         * @param importId The import id of the job
         * @return list of ImportEntity.Row or empty list
         */
        public List<Row> read(final int importId) {
            logger.debug("Reading import rows for importId={}", importId);

            try {
                List<ImportJobExhead> exheads = importJobExheadDAO.findByImportId(importId);
                logger.trace("importId={} exheads size={}", importId, exheads.size());

                final int numRowsPerImportJob = importJobContentsDAO.getNumRowsPerImportJob(importId) + 1;
                logger.trace("importId={} importRowsPerJob={}", importId, numRowsPerImportJob);

                //cache functions
                final Map<String, FieldConstant> cache = new HashMap<>();

                // read contents at once (instead of hitting row by row)
                final Map<Integer, List<ImportJobContents>> contentCache = new HashMap<>();
                final List<ImportJobContents> importJobContents = importJobContentsDAO.findByImportId(importId);

                for (int i = 0; i < importJobContents.size(); i++) {
                    List<ImportJobContents> existing = contentCache.get(importJobContents.get(i).getRow());

                    if (existing == null) {
                        existing = new ArrayList<>(); //TODO
                    }

                    existing.add(importJobContents.get(i));
                    contentCache.put(importJobContents.get(i).getRow(), existing);
                }
                logger.debug("Read content rows size={} for importId={}", importJobContents.size(), importId);

                final List<Row> importRows = new ArrayList<>(numRowsPerImportJob);
                final Set<Integer> set = contentCache.keySet();

                for (final int entry: set) {
                    final List<ImportJobContents> rowJobContents = contentCache.get(entry);
                    final Row row = new Import().new Row();

                    if (entry % 10000 == 0)  {
                        logger.debug("Read row={}", entry);
                    }

                    // Note: Gets all columns (including F104/F105 COLUMN)
                    for (int i = 0; i < rowJobContents.size(); i++) {
                        try {
                            final ImportJobExhead importJobExhead = exheads.get(i);
                            final String exhead = importJobExhead.getValue();

                            FieldConstant fieldConstant;

                            if (cache.containsKey(exhead)) {
                                fieldConstant = cache.get(exhead);
                            } else {
                                fieldConstant = FieldConstantUtil.toFieldConstant(exhead);

                                if (fieldConstant == null) {
                                    logger.trace("Field Constant null for headerValue={}", exhead);
                                }

                                cache.put(exhead, fieldConstant);
                            }

                            final ImportJobContents jobContents = rowJobContents.get(i);
                            row.getColumns().add(new Import().new Column<>(fieldConstant, jobContents.getValue()));
                        } catch (Exception e) {
                            logger.error("Error retrieving value={}", e); // throw ?
                        }
                    }
                    importRows.add(row);
                }
                return importRows;
            } catch (Exception e) {
                logger.error("Error", e);
            }

            return Collections.emptyList();
        }
    }



}
