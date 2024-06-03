package dev.roanoke.rib.cereal
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.serialization.json.jsonObject

object JsonConv {
    fun kotlinJsonToGsonJson(kotlinxJsonObject: JsonObject): com.google.gson.JsonObject {
        val jsonString = Json.encodeToString(JsonObject.serializer(), kotlinxJsonObject)
        return JsonParser.parseString(jsonString).asJsonObject
    }
    fun gsonToKotlinJson(gsonJsonObject: com.google.gson.JsonObject): JsonObject {
        val gson = Gson()
        val jsonString = gson.toJson(gsonJsonObject)

        return Json.parseToJsonElement(jsonString).jsonObject
    }
}