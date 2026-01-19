package helpers.apiHelpers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;

//Класс, создающий апи клиент непосредственно для обращения к серверу
//Объект этого класса создается в последнюю очередь, после создания объектов Builder и ApiRequest
//Пример использования:
//ApiClient client = new ApiClient("url");
//Response response = client.sendRequest(apiRequest);
@Log4j2
public class ApiClient {
    public final String baseUri;

    public ApiClient(String baseUri) {
        this.baseUri = baseUri;
    }

    public Response sendRequest(ApiRequest apiRequest) {
        RestAssured.baseURI = this.baseUri;
        RequestSpecification request = RestAssured.given();

        if (apiRequest.headers != null) {
            request.headers(apiRequest.headers);
        }

        if (apiRequest.params != null) {
            request.params(apiRequest.params);
        }

        if (apiRequest.body != null) {
            request.body(apiRequest.body);
        }

        switch (apiRequest.method.toLowerCase()) {
            case "get":
                return request.get(apiRequest.endpoint);
            case "post":
                return request.post(apiRequest.endpoint);
            case "put":
                return request.put(apiRequest.endpoint);
            case "delete":
                return request.delete(apiRequest.endpoint);
            case "patch":
                return request.patch(apiRequest.endpoint);
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + apiRequest.method);
        }
    }
}