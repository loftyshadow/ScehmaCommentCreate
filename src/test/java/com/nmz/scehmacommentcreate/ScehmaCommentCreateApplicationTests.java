package com.nmz.scehmacommentcreate;

import com.nmz.scehmacommentcreate.Utils.Filter;
import com.nmz.scehmacommentcreate.pojo.TableColumns;
import com.nmz.scehmacommentcreate.pojo.Tables_in_hklis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ScehmaCommentCreateApplicationTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    public void testeee(String tablename, Map map) throws Exception {
        String table = tablename;
        BufferedWriter bw = null;
        List<Tables_in_hklis> list1 = jdbcTemplate.query("SHOW TABLES IN hklis WHERE Tables_in_hklis = '" + table + "'", new BeanPropertyRowMapper<Tables_in_hklis>(Tables_in_hklis.class));

        if (list1.size() == 0)
            return;
        List<TableColumns> list = jdbcTemplate.query("show columns from " + table, new BeanPropertyRowMapper<TableColumns>(TableColumns.class));
        StringBuffer sb = new StringBuffer();
        for (TableColumns tableColumns : list) {
            if (map.get(tableColumns.getField()) != null) {

                sb.append("ALTER TABLE " + tablename + " MODIFY ");
                    sb.append("`" + tableColumns.getField() + "` " + tableColumns.getType());
                if ("NO".equals(tableColumns.getNull())) {
                    sb.append(" NOT NULL ");
                }
                sb.append(" COMMENT '" + map.get(tableColumns.getField()) + "';\n");
            }
        }
        bw = new BufferedWriter(new FileWriter("D:\\增加注释sql.sql", true));
        bw.write(sb.toString());
        bw.newLine();
        bw.close();
    }

    @Test
    public void getmap() throws Exception {
        String filePath = "D:\\schema\\";
        File file = new File(filePath);
        Filter filter = new Filter("Schema.java");
        String[] files = file.list(filter);

        boolean start = false;
        String tablename, finname, notes = "";
        for (String filename : files) {
            Map map = new HashMap<>();
            tablename = filename.substring(0, filename.indexOf("Schema"));
            finname = filePath + filename;
            FileReader reader = new FileReader(finname);
            BufferedReader fw = new BufferedReader(reader);
            String str;
            while (!(str = fw.readLine()).contains("FIELDNUM")) {
                if (str.contains("/**") && str.contains("*/")) {
                    notes = str.substring(str.indexOf("*") + 2, str.indexOf("*/")).trim();
                    if (!start) {
                        start = true;
                    }
                } else {
                    if (start) {
                        String[] clown = str.split(" ");
                        String clownname = clown[clown.length - 1];
                        if (!"".equals(clownname)) {
                            map.put(clownname.substring(0, clownname.length() - 1).toUpperCase(), notes);
                        }
                    }
                }
            }
            testeee(tablename, map);
            continue;
        }
        System.exit(0);
    }
}
