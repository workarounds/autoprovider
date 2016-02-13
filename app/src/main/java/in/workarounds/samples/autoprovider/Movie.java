package in.workarounds.samples.autoprovider;

import in.workarounds.autoprovider.AndroidId;
import in.workarounds.autoprovider.Column;
import in.workarounds.autoprovider.Table;

/**
 * Created by mouli on 11/5/15.
 */
@Table
public class Movie {
    @AndroidId @Column
    public Long id;
    @Column
    public String name;
    @Column
    public String director;

    SubObject object = new SubObject(1, "two");
    public Movie() {}

    public Movie(String name, String director) {
        this.name = name;
        this.director = director;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + director;
    }
}
