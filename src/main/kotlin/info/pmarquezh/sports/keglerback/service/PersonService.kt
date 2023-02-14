package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.model.person.Person
import info.pmarquezh.sports.keglerback.model.person.PersonDto

interface PersonService {
    /**
     * C
     */
    fun persistPerson(person: PersonDto);

    /**
     * RR
     */
    fun retrievePersons(): List<Person>;

    /**
     * R
     */
    fun retrievePerson(personId: String): Person;

    /**
     * U
     */
    fun updatePerson(personId: String, person: Person);

    /**
     * D
     */
    fun deletePerson(personId: String);
}