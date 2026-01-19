package helpers.apiHelpers;

import java.util.Map;

//Класс-строитель запроса для апи, заполняет объект для построения запроса всеми необходимыми данными
//Объект этого класса нужно создать до момента отправки запроса на сервер и передать его в конструктор класса ApiRequest
//Пример использования:
//Builder builder = new Builder()
//        .method("get")
//        .endpoint("/your-endpoint")
//        .headers(Map.of("Authorization", "Bearer your_token_here"))
//        .params(Map.of("param1", "value1", "param2", "value2"));
public class Builder {
    public String method;
    public String endpoint;
    public Map<String, String> headers;
    public Map<String, Object> params;
    public String body;

    public Builder method(String method) {
        this.method = method;
        return this;
    }

    public Builder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Builder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Builder params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Builder body(String body) {
        this.body = body;
        return this;
    }

    public ApiRequest build() {
        return new ApiRequest(this);
    }
}