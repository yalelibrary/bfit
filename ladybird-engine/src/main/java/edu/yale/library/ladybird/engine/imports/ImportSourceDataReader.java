package edu.yale.library.ladybird.engine.imports;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import edu.yale.library.ladybird.engine.oai.DatafieldType;
import edu.yale.library.ladybird.engine.oai.Marc21Field;
import edu.yale.library.ladybird.engine.oai.MarcReadingException;
import edu.yale.library.ladybird.engine.oai.OaiHttpClient;
import edu.yale.library.ladybird.engine.oai.OaiProvider;
import edu.yale.library.ladybird.engine.oai.Record;
import edu.yale.library.ladybird.engine.oai.SubfieldType;
import edu.yale.library.ladybird.entity.FieldMarcMapping;
import edu.yale.library.ladybird.entity.ImportSourceData;
import edu.yale.library.ladybird.entity.ImportSourceDataBuilder;
import edu.yale.library.ladybird.persistence.dao.ImportSourceDataDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportSourceDataHibernateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ImportSourceDataReader {

    private Logger logger = LoggerFactory.getLogger(ImportSourceDataReader.class);

    final ImportSourceDataDAO importSourceDataDAO = new ImportSourceDataHibernateDAO();


    // --------------------------------------------------------------
    // Used by ImportWriter
    //---------------------------------------------------------------

    /**
     * Hits OAI feed and gets a Record
     *
     * @param bibIds
     * @param marc21FieldMap
     * @return
     */
    public Map<String, Multimap<Marc21Field, ImportSourceData>> readBibIdMarcData(final OaiProvider oaiProvider,  List<String> bibIds,
                                                                                  final Map<Marc21Field, FieldMarcMapping> marc21FieldMap, final int importId) {
        logger.debug("Reading marc data for the bibIds");

        final Map<String, Multimap<Marc21Field, ImportSourceData>> bibIdMarcValues = new HashMap<>();

        for (final String id : bibIds) {
            final OaiHttpClient oaiClient = new OaiHttpClient(oaiProvider);
            try {

                final Record recordForBibId = oaiClient.readMarc(id); //Read OAI feed
                final Multimap<Marc21Field, ImportSourceData> marc21Values = buildMap(recordForBibId, importId);

                //logger.debug("Marc for bibId={} equals={}", id, marc21Values.toString());

                bibIdMarcValues.put(id, marc21Values);
            } catch (IOException e) {
                logger.error("Error reading source", e);
            } catch (MarcReadingException e) {
                logger.error("Error reading oai marc record", e);
            }
        }
        return bibIdMarcValues;
    }

    /**
     * @param record MarcRecord
     * @return a map(k,v) where k=Tag, v=ImportSourceData
     */
    private Multimap<Marc21Field, ImportSourceData> buildMap(final Record record, final int importId) {
        final List<DatafieldType> datafieldTypeList = record.getDatafield();
        final Multimap<Marc21Field, ImportSourceData> attrMap = HashMultimap.create();

        for (final DatafieldType type : datafieldTypeList) {
            final String tag = type.getTag();

            //Get subfields:
            final List<SubfieldType> subfieldTypeList = type.getSubfield();

            //Get k2 values
            for (final SubfieldType s : subfieldTypeList) {
                final String code = s.getCode(); //e.g "a", "c", "d"
                final String codeValue = s.getValue(); //e.g. "(oOCoLC) ocn709288147"

                final ImportSourceData importSourceData =
                        new ImportSourceDataBuilder().setK1(tag).setK2(code).setValue(codeValue).
                                setZindex(0).setDate(new Date()).setImportSourceId(importId) //pass date
                                .createImportSourceData(); //FIXME zindex, date
                attrMap.put(getMar21FieldForString(tag), importSourceData);
            }
        }
        return attrMap;
    }


    /**
     * @param tag
     * @return
     */
    public Marc21Field getMar21FieldForString(final String tag) {
        final String TAG_ID = "_"; //TODO
        try {
            Marc21Field marc21Field = Marc21Field.valueOf(TAG_ID + tag);
            return marc21Field;
        } catch (IllegalArgumentException e) {
            //ignore fields since need to fill the Mar21Field list
            logger.error(e.getMessage());
            return Marc21Field.UNK;
        }
    }

    // --------------------------------------------------------------
    // Used by ExportReader
    //---------------------------------------------------------------

    /**
     * Reads import source data tables and constructs a map (k,v), where k=rowNo, v=map(s,t), where s=Mar21Tag,t=Marc21DataFieldType
     *
     * TODO test
     *
     * @see edu.yale.library.ladybird.engine.exports.ExportReader#getImportJobContents
     * @param importId import id of job
     * @param expectedNumRowsToWrite total number of expected rows.
     * @return  map (int, multimap (marc21 field, map(string,string)
     */
    public Map<Integer, Multimap<Marc21Field, Map<String, String>>> readBibIdData(final int importId,
                                                                                   final int expectedNumRowsToWrite) {
        logger.debug("Reading from import source table, and populating the marc value map. "
                + "ImportId={}, ExpectedNumRowsToWrite={}", importId, expectedNumRowsToWrite);

        final Map<Integer, Multimap<Marc21Field, Map<String, String>>> map = new HashMap<>();

        for (int i = 0; i < expectedNumRowsToWrite; i++) {
            final List<ImportSourceData> importSourcesList = importSourceDataDAO.findByImportId(importId, i);
            //logger.debug("ImportSourceDataList={}", importSourcesList.toString());
            map.put(i, marshallMarcData(importSourcesList));
        }

        //logger.debug("All import source contents={}" + importSourceDataDAO.findAll().toString());
        return map;
    }

    /**
     * @see #readBibIdData for invocation
     * @param importSourceDataList
     * @return
     */
    public Multimap<Marc21Field, Map<String, String>> marshallMarcData(final List<ImportSourceData> importSourceDataList) {
        logger.debug("Marshalling marc data from import source data.");

        //final Map<Marc21Field, DatafieldType> marcTagData = new HashMap<>();

        //populate map (e.g. (245,{a,"text"}), (245, {b,"text b"}). This map will then be read.
        final Multimap<Marc21Field, Map<String, String>> map = HashMultimap.create(); //k=tag, v={subfield,value}

        for (int i = 0; i < importSourceDataList.size(); i++) {
            final ImportSourceData entry = importSourceDataList.get(i);
            final String k1 = entry.getK1();

            switch (k1) {
                case "880":
                    //logger.debug("Ignoring field 880");
                    break;
                default:
                    logger.debug("Putting field={} value={}", k1, entry.getValue());
                    final Map<String, String> attrValue = new HashMap<>();
                    attrValue.put(entry.getK2(), entry.getValue());
                    map.put(Marc21Field.valueOfTag(k1), attrValue);
                    break;
            }
        }
        return map;
    }

}
