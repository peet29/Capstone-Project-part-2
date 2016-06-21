package me.hanthong.capstone.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by peet29 on 17/6/2559.
 */
@Database(version = NewsDatabase.VERSION)
public final class NewsDatabase {

    public static final int VERSION = 2;

    @Table(NewsColumns.class)
    public static final String LISTS = "lists";
}
