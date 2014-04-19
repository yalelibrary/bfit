package edu.yale.library.ladybird.engine;

import com.google.common.collect.Multimap;
import edu.yale.library.ladybird.engine.imports.ImportEntity;
import edu.yale.library.ladybird.engine.imports.ImportWriter;
import edu.yale.library.ladybird.engine.model.FieldConstant;
import edu.yale.library.ladybird.engine.model.FunctionConstants;
import edu.yale.library.ladybird.engine.model.Marc21Field;
import edu.yale.library.ladybird.engine.oai.OaiProvider;
import edu.yale.library.ladybird.kernel.beans.ImportSourceData;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImportWriterTest {

    @Test
    public void shouldFindF104Column() {
        final ImportWriter importWriter = new ImportWriter();
        final List<ImportEntity.Row> rowList = new ArrayList<>();
        final ImportEntity.Row row = new ImportEntity().new Row();
        final FieldConstant f1 = FunctionConstants.F104;
        final ImportEntity.Column<String> column1 = new ImportEntity().new Column<>(f1,
                String.valueOf("2222"));
        row.getColumns().add(column1);
        final FieldConstant f2 = FunctionConstants.F1;
        final ImportEntity.Column<String> column2 = new ImportEntity().new Column<>(f2,
                String.valueOf("2222"));
        row.getColumns().add(column2);
        assert (f1 == f1);
        assert (f1 != f2);
        assert (column1 == column1);
        assert (column1 != column2);
        rowList.add(row);
        assert (importWriter.findColumn(rowList, f1) == 0);
        rowList.remove(0);
        assert (importWriter.findColumn(rowList, f1) == -1);
    }

    @Test
    public void shouldMatchExpectedBibIds() {
        final ImportWriter importWriter = new ImportWriter();
        final ImportEntity.Row row = new ImportEntity().new Row();
        final FieldConstant f104 = FunctionConstants.F104;
        final ImportEntity.Column<String> column1 = new ImportEntity().new Column<>(f104, String.valueOf("2222"));

        row.getColumns().add(column1);

        assert (row.getColumns().size() == 1);

        final List<String> bibIds = importWriter.readBibIdsFromColumn(Collections.singletonList(row), (short) 0);

        assert (bibIds.size() == 1);
        assert (bibIds.get(0).equals("2222"));

        final FieldConstant f2 = FunctionConstants.F1;
        final ImportEntity.Column<String> column2 = new ImportEntity().new Column<>(f2,
                String.valueOf("555"));
        row.getColumns().add(column2);

        final List<String> bibIds2 = importWriter.readBibIdsFromColumn(Collections.singletonList(row), (short) 1);
        assert (bibIds2.size() == 0);

        final List<String> bibIds3 = importWriter.readBibIdsFromColumn(Collections.singletonList(row), (short) 0);
        assert (bibIds3.size() == 1);

        row.getColumns().clear();
        row.getColumns().add(column2);

        final List<String> bibIds4 = importWriter.readBibIdsFromColumn(Collections.singletonList(row), (short) -1);
        assert (bibIds4.size() == 0);
    }


    @Test
    public void shoudEqualOAIFunction() {
        final ImportWriter importWriter = new ImportWriter();
        final FieldConstant f104 = FunctionConstants.F104;
        final ImportEntity.Column<String> column1 = new ImportEntity().new Column<>(f104, String.valueOf("2222"));

        assert (importWriter.isOAIFunction(column1));

        final FieldConstant f1 = FunctionConstants.F1;
        final ImportEntity.Column<String> column2 = new ImportEntity().new Column<>(f1, String.valueOf("2222"));

        assert (!importWriter.isOAIFunction(column2));
    }


    @Test
    public void shouldContainBibIdMarcTags() {
        final ImportWriter importWriter = new ImportWriter();
        final PropUtil util = new PropUtil();
        final OaiProvider provider = new OaiProvider("id",
                util.getProperty("oai_test_url_prefix"),
                util.getProperty("oai_url_id"));
        importWriter.setOaiProvider(provider);
        final String bibId = "9807234";
        final List<String> bibIds = Collections.singletonList(bibId);
        final Map<String, Multimap<Marc21Field, ImportSourceData>> map = importWriter.readBibIdMarcData(bibIds, null, 0); //FIXME params null and 0 for importid
        assertEquals("Map size mismatch", map.size(), 1);
        final Multimap<Marc21Field, ImportSourceData> innerMap = map.get(bibId);
        Collection<ImportSourceData> collection = innerMap.get(Marc21Field._520);
        final Iterator<ImportSourceData> it = collection.iterator();

        String text = "";
        while (it.hasNext()) {
            ImportSourceData importSourceData = it.next();
            text = importSourceData.getValue();
        }

        assertTrue("Text missing", text.contains("by the sun, the moon, the stars"));
        //TODO Test other values/mappings here. . .
    }

    @Test
    public void shouldTranslateToMarc21Field() {
        final ImportWriter importWriter = new ImportWriter();
        final Marc21Field marc21Field = importWriter.getMar21FieldForString("245");
        assert(marc21Field.equals(Marc21Field._245));
    }

    @Test
    public void shouldFindColumn(){
        final ImportWriter importWriter = new ImportWriter();
        final List<ImportEntity.Row> rowList = new ArrayList<>();
        final ImportEntity.Row row = new ImportEntity().new Row();
        final FieldConstant f1 = FunctionConstants.F104;
        final ImportEntity.Column<String> column1 = new ImportEntity().new Column<>(f1,
                String.valueOf("2222"));
        row.getColumns().add(column1);
        final FieldConstant f2 = FunctionConstants.F1;
        final ImportEntity.Column<String> column2 = new ImportEntity().new Column<>(f2,
                String.valueOf("2222"));
        row.getColumns().add(column2);
        assert (f1 == f1);
        assert (f1 != f2);
        assert (column1 == column1);
        assert (column1 != column2);
        rowList.add(row);
        System.out.println(rowList);
        assert (importWriter.findColumn(rowList, f1) == 0);
        assertEquals ("Value mismatch", importWriter.findColumn(rowList, f2), 1);
    }
    /**
     * General utility. Subject to removal.
     */
    public class PropUtil {
        final Properties prop;

        {
            prop = new Properties();
            InputStream input = null;

            try {
                input = PropUtil.class.getResourceAsStream("/ladybird.properties");
                prop.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public String getProperty(String p) {
            return prop.getProperty(p);
        }
    }

}
