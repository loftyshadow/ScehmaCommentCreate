package com.nmz.scehmacommentcreate.Utils;

import java.io.File;
import java.io.FilenameFilter;

public class Filter implements FilenameFilter {

    private String suffix;
    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(suffix);
    }

    public Filter(String suffix) {
        this.suffix = suffix;
    }
}
