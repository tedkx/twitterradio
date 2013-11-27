package models.db;

import java.util.List;

import play.db.ebean.*;

import javax.persistence.*;

@Entity
public class Account extends Model {

	private static final long serialVersionUID = 6004345749334470262L;

	@Id
	public long accountID;
	public String fullName;
    public String token;
    public String secret;
    public int trendsBandID;
    
    @OneToMany(cascade = CascadeType.ALL)
    @Column(nullable = true)
    public List<Band> bands;
    public static Finder<Long,Account> find = new Finder<Long,Account>(Long.class, Account.class);
}
