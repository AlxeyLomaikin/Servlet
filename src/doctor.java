
import java.io.Serializable;

/**
 * Created by User on 26.10.2015.
 */
public class doctor implements Serializable {
    private int ID;
    private String name;
    private String surname;
    private String occupation;
    private int age;
    public doctor() {
    }
    public doctor(int id, String name, String surname, String occupation, int age) {
        this.ID = id;
        this.name = name;
        this.surname = surname;
        this.occupation = occupation;
        this.age = age;
    }
    public int getID () {return ID;}
    public void setID (int ID) {this.ID = ID;}
    public String getName() {return name;}
    public void setName (String name) {this.name = name;}
    public String getSurname () {
        return surname;
    }
    public void setSurname (String surname){
        this.surname = surname;
    }
    public String getOccupation () {
        return occupation;
    }
    public void setOccupation (String occupation){
        this.occupation = occupation;
    }
    public int getAge () {return age;}
    public void setAge (int age){
        this.age = age;
    }
}