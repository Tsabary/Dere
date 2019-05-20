package co.getdere.models

class Itineraries (val id : String, val publishedStatus : Boolean, val creator : String, val title : String, val images : MutableList<String>, val description : String, val coverimage : String, val video : String) {
    constructor() : this("", false, "","", mutableListOf(), "", "", "")
}
