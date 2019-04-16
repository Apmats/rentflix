## Rentflix, a demo application

## Intro

This is a demo application designed to expose a web API that maintains a list of users and movies, and allows users to rent said movies making the appropriate assignments.

The exposed actions are:

a) For a customer, rent a list of movies (by their ID). If everything goes well, then a total price is returned. The customer account is updated with a number of bonus points corresponding to the movies being rented.
b) For a customer, return a list of already rented physical copies. If everything goes well, then a total surcharge cost is returned.
c) For a customer, get some basic details, such as their name and bonus points.
d) For a customer, get some movie recommendations based on what similar users interacted with.

## Steps to Setup

**Bring up PostgreSQL**

Bring up the dockerized PostgreSQL database by running docker-compose up under the resources directory:

```bash
docker-compose -d up
```

This brings up the docker container with a postgresql instance running in the background, ready to be used by the app itself.

**Run the app**

Type the following command from the root directory of the project to run it:

```bash
mvn spring-boot:run
```

**Manual testing**

On application start, the data.sql file found under the resources directory is run and seeds the database with some data.

We can then perform requests like the following:

```console
apmats@apmats:~/Projects/rentflix$ curl -H "Content-Type: application/json" -X POST -d "[1,2,4]" http://localhost:8080/customer/1/rent
{"total_cost":"110.0","physical_copy_ids":[1,3,9]}
```

```console
apmats@apmats:~/Projects/rentflix$ curl -H "Content-Type: application/json" -X POST -d "[1,2]" http://localhost:8080/customer/2/rent
{"total_cost":"80.0","physical_copy_ids":[2,4]}
```

```console
apmats@apmats:~/Projects/rentflix$ curl -H "Content-Type: application/json" -X POST -d "[1,2]" http://localhost:8080/customer/1/return
{"total_surcharge":0.0}
```

And we can after having a few interactions for a customer and sufficient data in the system, do a request like this:

```console
apmats@apmats:~/Projects/rentflix$ curl -H "Content-Type: application/json" -X POST -d "1" http://localhost:8080/customer/2/recommendations
{"recommended_movie_ids":[4]}
```

where the movie rented by used 1 but not rented by user 2 is recommended to user 2 because these users have similar history.


Finally we can get some details for each customer:


```console
apmats@apmats:~/Projects/rentflix$ curl -H "Content-Type: application/json" -X GET http://localhost:8080/customer/2/details
{"bonus_points":"4","full_name":"Gary Oldman"}
```


**Automated testing**

Automated tests can be run by running the command:

```bash
mvn test
```

Roughly there are some tests for the classes holding the main application logic. 
We test the rent and return functionality, the cost calculations and the recommendation functionality.

Due to time constraints, the endpoints themselves aren't tested, but would be in a proper implementation.


**Project structure details**

The 3 classes found under controllers handle the endpoints and routing of arguments to the service classes. Although for simplicity's sake, the CustomerDetailsController performs the customer retrieval itself, it would possibly be extracted into a service when our logic for that sort of action grows.

The model directory holds a few ORM classes represented in our database tables. The repositories under the repository directory hold the retrieval methods or these.

Finaly, under the service and util directories, most of our logic resides in the classes performing most of the work for this application.

**Design choices**

When designing the API, despite this not being RESTful, I decided to keep the user ID as part of the URL. It feels more straightforward that way to me, as then a single kind of argument needs to be provided in the POSTed data in all cases.

I decided to go with a design of having Movie entries that describe a film, and Physical Media entries which represent a physical copy of a movie. On rent time, a user requesting to rent a movie gets assigned an available copy.

The assigned IDs are returned with the rent request. So then the API user knows which copies it needs to return when the time comes.

I decided to capture the information of these assignments ("rentals") in a separate database table, holding the dates of rental and return, and holding this information in the database.
Even though strictly not needed (this information could be stored in the table representing the physical copies) a design like this opens up options of using that historical data for other purposes.
I already implemented a movie suggestion feature based on that, we could easily provide analytics for the administrator by generating charts or graphs, or extract statistics that can be communicated to the user eg. "currently popular movies".

No authentication was used, with the assumption that this will be simply the backend of either a more complex application that could handle authorization itself before forwarding requests, or that it will be used as a local store installation operated by an authorized operator.


**Notes on what was done and what wasn't**

I spent over 10 hours on this so I wanted to avoid putting in a huge amount of effort in what is a preliminary task in the evaluation process. So there are tests missing for the controllers which would be good to have. 
Invalid input in the POST requests is handled poorly (ugly messages get returned instead of an explanation of the issue).
The API could be documented better, perhaps by using an automated tool for this.

Obviously, interacting with the application, with only the currently exposed endpoints, requires having access to the database to query for the list of movies, for their pricing, what is currently available etc.
More end points should be exposed to allow access to this information instead.
