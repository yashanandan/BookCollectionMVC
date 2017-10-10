# CRUD Application - Book Collection
This simple CRUD Application lists books in my collection
- There are two books by default
- New Books can be added.
- Existing books can be modified, deleted

# Features
## MVC Framework
- [VertX](http://vertx.io/) framework.
- It is open source, polyglot ( can be coded in Java, JS, Scala, Ruby, Kotlin, Groovy)
- Light weight, Highly Scaleable
- It is flexible for creating both ReST Microservice or full fledged web applications
- The community is picking up and there are active discussions about this framework
- It is highly customisable and the web application can be coded per your need and no need to compromise for lack of features.
- Both an API and a Web GUI is created in a single framework without extra work or time.
- It has its own testing libraries (VertX Unit) which is similar to JUnit.
## Build Tool
- Gradle is used as build tool due to its ease of use.
- Build time is less compared to maven or sbt
## Database Used
- MySQL is used as the database due to its simplicity and lightweightedness
- Easy to deploy and is highly scalable to the increasing users.
## CSS Framework
- Bootstrap is used as the CSS framework for it the widely used and has the ability to make good looking GUI with quick turn around.
- It was developed by Twitter and is now an open source project
  
# Using the application
Once cloned there are two ways to run the application.
### Using Eclipse ( or any IDE )
```
Import the repo as Gradle Project ( All the dependencies will be downloaded automatically )
Run -> Run Configurations -> Select the Project ( BookCollection MVC )
Main Class -> io.vert.core.Launcher
Under the Arguments tab -> Program Arguments -> run com.indbiz.books.verticles.BooksVerticle -conf conf/application.conf
```
### Build fatjar
```
In the shell  ( Terminal or Command Prompt ) install gradle
Type 'gradle fatJar'
Run the application using the command 'java -jar build\libs\BookCollectionMVC-1.0.jar -conf conf\application.conf'
```
The application goes live under the url - [http://localhost:8082](http://localhost:8082).

### Browsing through the UI
[http://localhost:8082](http://localhost:8082) will take you to the welcome page
- `/ui` Will take you to the UI of the CRUD Application
- `GET /api/books` Will fetch all the books in the database.
- `POST /api/books` WIll add a new book to the database
- `PATCH /api/books/bookId` Will update the book with the specified bookId
- `DELETE /ap/books/bookId` Will deleted the book with the specified bookId

### Format of Data when using API
```
{
"name" : "I Do What I Do",
"author" : "Raghuram Rajan",
"genre" : "Non-Fiction, Economics",
"status" : "Bought, yet to read"
}
```
### Format of application.conf
```
{
  "url": "jdbc:mysql://localhost/books?characterEncoding=UTF-8&useSSL=false",
  "driver_class": "com.mysql.cj.jdbc.Driver",
  "user": "books",
  "password": "books",
  "max_pool_size": 30
}
```

# Drawbacks in the current App
- HTTP Port is not configurable ( unless changed in the code ). It runs default in 8082
- Every time `/ui` is accessed two books will be added by default ( Request is made every time page is refreshed or accessed again )
- Duplicates are not checked
- Bulk upload is not supported
