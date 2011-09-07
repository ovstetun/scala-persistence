package no.ovstetun.jdummy;

import java.util.Date;

public class Person {
    private String firstname;
    private String lastname;
    private Date birthDate;
    private Gender gender;

    public Person(String firstname, String lastname, Date birthDate, Gender gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
}

interface PersonRepository {
    /**
     * @return A person if found, <code>null</code> otherwise.
     */
    Person findById(int id);
}
