package co.getdere.Models

class ReputationScore(val postId: String, val initiatorId : String, val points : Int){
    constructor() : this ("", "", 0)
}