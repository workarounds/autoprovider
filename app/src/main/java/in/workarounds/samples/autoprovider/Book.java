package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AutoIncrement;
import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.NotNull;
import in.workarounds.autoprovider.PrimaryKey;
import in.workarounds.autoprovider.Table;

/**
 * Created by madki on 08/10/15.
 */
@Table
public class Book {
    @PrimaryKey @AutoIncrement @Column("_id") Long id;
    @NotNull @Column String name;
    @Column String author;
    @Column Float rating;
}
