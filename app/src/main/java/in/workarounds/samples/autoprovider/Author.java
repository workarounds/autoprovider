package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AndroidId;
import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.Table;

/**
 * Created by mouli on 11/5/15.
 */
@Table
public class Author {
    @AndroidId @Column
    public Long id;
    @Column
    public String name;
}
