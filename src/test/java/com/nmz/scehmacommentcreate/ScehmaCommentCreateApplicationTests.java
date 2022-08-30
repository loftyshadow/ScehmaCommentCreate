package com.nmz.scehmacommentcreate;

import com.nmz.scehmacommentcreate.Utils.Filter;
import com.nmz.scehmacommentcreate.pojo.TableColumns;
import com.nmz.scehmacommentcreate.pojo.Tables_in_hklis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ScehmaCommentCreateApplicationTests {
    @Value("${database}")
    String datebase;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    public void createSQL(String tablename, Map<String, String> map) throws Exception {
        BufferedWriter bw = null;
        /*判断是否在数据库中存在对应schema的表*/
        List<Tables_in_hklis> existTable = jdbcTemplate.query("SHOW TABLES IN " + datebase + " WHERE Tables_in_" + datebase +" = '" + tablename + "'", new BeanPropertyRowMapper<Tables_in_hklis>(Tables_in_hklis.class));
        if (existTable.size() == 0) {
            return;
        }
        List<TableColumns> list = jdbcTemplate.query("SHOW COLUMNS FROM " + tablename, new BeanPropertyRowMapper<TableColumns>(TableColumns.class));
        StringBuilder sb = new StringBuilder();
        for (TableColumns tableColumns : list) {
            if (map.get(tableColumns.getField()) != null) {
                sb.append("ALTER TABLE ").append(tablename).append(" MODIFY ");
                sb.append("`").append(tableColumns.getField()).append("` ").append(tableColumns.getType());
                if ("NO".equals(tableColumns.getNull())) {
                    sb.append(" NOT NULL ");
                }
                sb.append(" COMMENT '").append(map.get(tableColumns.getField())).append("';\n");
            }
        }
        bw = new BufferedWriter(new FileWriter("D:\\增加注释sql.sql", true));
        try {
            bw.write(sb.toString());
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void getMap() throws Exception {
        String filePath = "D:\\schema\\";
        File file = new File(filePath);
        Filter filter = new Filter("Schema.java");
        String[] files = file.list(filter);
        String tablename, finname, notes = "";
        for (String filename : files) {
            Map<String, String> map = new HashMap<>();
            tablename = filename.substring(0, filename.indexOf("Schema"));
            finname = filePath + filename;
            FileReader reader = new FileReader(finname);
            BufferedReader fw = new BufferedReader(reader);
            String str;
            while (!(str = fw.readLine()).contains("FIELDNUM")) {
                if (str.contains("/**") && str.contains("*/")) {
                    notes = str.substring(str.indexOf("*") + 2, str.indexOf("*/")).trim();
                } else {
                        String[] clown = str.split(" ");
                        String clownname = clown[clown.length - 1];
                        if (!"".equals(clownname)) {
                            map.put(clownname.substring(0, clownname.length() - 1).toUpperCase(), notes);
                        }
                    }
            }
            createSQL(tablename, map);
            continue;
        }
        System.exit(0);
    }
}
