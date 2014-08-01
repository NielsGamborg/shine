package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity 
@Table(name="corpus")
public class Corpus extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    public Long id;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String name;

    public String metaData;

    public String summary;

    public String tags;
    
    public String justification;

    @Constraints.Required
    @Formats.NonEmpty
    public Long user_id;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="corpus")
    public List<Resource> resources = new ArrayList<Resource>();
    
    @Version
    public Timestamp lastUpdate;

    public static Model.Finder<String,Corpus> find = new Model.Finder<String,Corpus>(String.class, Corpus.class);

    
    public Corpus(String name, Long user_id) {
		super();
		this.name = name;
		this.user_id = user_id;
	}

    public static List<Corpus> findByUser(User user) {
        return find.where()
                .eq("user_id", user.id)
                .findList();
    }
    
    public static List<Corpus> findAll() {
        return find.all();
    }

    public static Corpus find(Long id) {
    	return find.where().eq("id", id).findUnique();
    }

    /**
     * Create a new corpus.
     */
    public static Corpus create(String name, Long userId) {
    	Corpus corpus = new Corpus(name, userId);
    	corpus.save();
		return corpus;
   }
}