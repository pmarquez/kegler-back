package info.pmarquezh.sports.keglerback.service

import info.pmarquezh.sports.keglerback.model.person.Person
import info.pmarquezh.sports.keglerback.model.person.PersonDto
import org.springframework.stereotype.Service

@Service
class PersonServiceImpl: PersonService {

    /**
     * C
     */
    override fun persistPerson(person: PersonDto) {
        TODO("Not yet implemented")
    }

    /**
     * RR
     */
    override fun retrievePersons(): List<Person> {
        TODO("Not yet implemented")
    }

    /**
     * R
     */
    override fun retrievePerson(personId: String): Person {
        TODO("Not yet implemented")
    }

    /**
     * U
     */
    override fun updatePerson(personId: String, person: Person) {
        TODO("Not yet implemented")
    }

    /**
     * D
     */
    override fun deletePerson(personId: String) {
        TODO("Not yet implemented")
    }
}
