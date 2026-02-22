package com.itsfrz.tictactoe.common.functionality

object Generate{

    fun uniqueId(data : String) : String {
        if (data.length <= 10)
            return data
        val s1 = data.substring(0,(data.length/2)-1)
        val s2 = data.substring(data.length/2,data.length)

        val uniqueNumber = data.substring(data.length-5,data.length)
        val uniqueCharacters = getUniqueCharacters(data.substring(data.length-3,data.length))
        return "$uniqueCharacters$uniqueNumber"
    }

    private fun getUniqueCharacters(data: String): String {
        var characterKey = ""
        data.forEach {
            characterKey += getCharacterKey(it)
        }
        return characterKey
    }

    private fun getCharacterKey(digit: Char): Char {
        return when(digit){
          '0'-> 'A'
          '1'-> 'X'
          '2'-> 'F'
          '3'-> 'D'
          '4'-> 'R'
          '5'-> 'N'
          '6'-> 'Y'
          '7'-> 'Q'
          '8'-> 'W'
          '9'-> 'T'
          else -> 'P'
        }
    }

}