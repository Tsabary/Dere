package co.getdere.models

class Buckets(
    val id: String,
    val public: Boolean,
    val creator: String,
    val title: String,
    val description: String,
    val images: Map<String, Boolean>,
    val timestampCreated: Long) {

    constructor() : this("", false, "", "", "", mapOf(), 0)

}
