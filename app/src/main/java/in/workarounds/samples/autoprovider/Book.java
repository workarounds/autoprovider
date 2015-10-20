package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AutoIncrement;
import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.NotNull;
import in.workarounds.autoprovider.PrimaryKey;
import in.workarounds.autoprovider.Table;

/**
 * Created by madki on 08/10/15.
 */
@Table(name = "BookTable")
public class Book {
    @PrimaryKey @AutoIncrement @Column("_id")
    public Long id;
    @NotNull @Column
    public String name;
    @Column
    public String author;
    @Column
    public Float rating;
}
