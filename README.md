# jetty-crud
Simple [CRUD](https://en.wikipedia.org/wiki/Create,_read,_update_and_delete) Web Server using Jetty.

If you search around the web for examples of running a simple web server using [Jetty](https://www.eclipse.org/jetty/) all the popular results use complicated tools like [maven](https://maven.apache.org/) and [gradle](https://gradle.org/) which need gigantic configuration files, sometimes in written in XML (ugh!), lots of external dependencies (I'm looking at your [Spring Boot](https://spring.io/projects/spring-boot)) and all sorts of crud!

Does a simple CRUD based application need so much code/configuration? Why is there a need for all this magic?

So I set out to creat the simplest CRUD server using Jetty and nothing else.

Inspired by [jetty documentation](https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html) and [simple-webserver-and-rest-with-jetty-and-no-xml](https://github.com/mcaprari/simple-webserver-and-rest-with-jetty-and-no-xml)


## Installation

You'll need to download bunch of [Jetty jars]() to make this work.

One simpler solution is to use the [`jetty-all-uber.jar`]() which contains ALL the jetty jars.

You can get it using :


```bash
curl -o jetty-all-uber.jar https://repo1.maven.org/maven2/org/eclipse/jetty/aggregate/jetty-all/9.4.12.v20180830/jetty-all-9.4.12.v20180830-uber.jar
```

## Running

This will compile and run a local server. The server by default runs on port 8080.

```bash
javac -d classes -cp jetty-all-uber.jar MinimalCrud.java && java -cp classes:jetty-all-uber.jar org.eclipse.jetty.embedded.MinimalCrud
```


## Accessing

The minimal Jetty server just accepts all HTTP connections and logs them and returns a [HTTP 200](https://httpstatuses.com/200) status code.

The following bits of information are logged.

	- Path
	- Content-Type
	- Body (if applicable)

These [curl](https://curl.haxx.se/) commands can help you test the server

### GET

GET with a simple [query string](https://en.wikipedia.org/wiki/Query_string)

```bash
	curl "localhost:8080/foo?name=ferret"
```

will yield

```
GET at path : /foo
- query string : name=ferret
```

### PUT

PUT with a [x-www-form-urlencoded](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST) key-value pairs like a [HTML form](https://developer.mozilla.org/en-US/docs/Learn/HTML/Forms/Your_first_HTML_form)

```bash
	curl -X PUT -d "{value:20}" "localhost:8080/foo"
```

will yield

```
PUT at path : /foo
- query string : null
- with Content-Type : application/x-www-form-urlencoded
- Body : {value:20}
```

### POST

POST with a simple [query string](https://en.wikipedia.org/wiki/Query_string)

```bash
	curl -X POST "localhost:8080/asdad?name=ferret"
```

will yield

```
POST at path : /asdad
- query string : name=ferret
- Content-Type : null
- Body :
```

POST with a [x-www-form-urlencoded](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/POST) key-value pairs like a [HTML form](https://developer.mozilla.org/en-US/docs/Learn/HTML/Forms/Your_first_HTML_form)

```bash
	curl -X POST -d "{value:20}" "localhost:8080/bar/foo"
```

will yield

```
POST at path : /bar/foo
- query string : null
- Content-Type : application/x-www-form-urlencoded
- Body : {value:20}
```

POST with a file attachment using [HTTP multipart formposts](https://ec.haxx.se/http-multipart.html)

```bash
	curl -F 'data=MinimalCrud.java' "localhost:8080/foo/bar"
```

will yield

```
POST at path : /foo/bar
- query string : null
- Content-Type : multipart/form-data; boundary=------------------------193fa21a346ad4ca
- Body : --------------------------193fa21a346ad4caContent-Disposition: form-data; name="data"MinimalCrud.java--------------------------193fa21a346ad4ca--
```

> Note : This is not the full body of the file, just the first part of the HTTP multipart formpost


### DELETE

```bash
	curl -X DELETE "localhost:8080/bar"
```

will yield

```
DELETE at path /bar
```
