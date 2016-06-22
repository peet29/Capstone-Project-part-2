package me.hanthong.capstone.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by peet29 on 17/6/2559.
 */
public interface NewsColumns {

    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";

    @DataType(TEXT)
    @NotNull
    String TITLE = "title";

    @DataType(TEXT)
    @NotNull
    String LINK = "link";

    @DataType(TEXT)
    @NotNull
    String DATE = "date";

    @DataType(TEXT)
    @NotNull
    String DESCRIPTION = "description";

    @DataType(TEXT)
    @NotNull
    String PHOTO = "photo";

    @DataType(INTEGER)
    @NotNull
    String FAV = "fav";
}
