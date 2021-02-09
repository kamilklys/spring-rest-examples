# spring-rest-examples

# Running application
Simply run main method in RestApplication.java.

# How to use application

Application operates on some very simple and dummy objects representing `Accounts` entity.
REST API supports multiple operations.

## Create account resource
````
POST /api/accounts
{
	"balance":1234.0,
	"owner":"b4167fba-5ed8-44c0-bc3c-c8dbf94c4092"
}
````

## Get account resource
````
GET /api/accounts/<id>
````

## Get all account resources
````
GET /api/accounts
````
This endpoint supports filtering by Account ID:
````
GET /api/accounts?id=<id>
````

## Replace account resource
````
PUT /api/accounts/<id>
{
    "id": "152e5972-754f-46b1-9025-d3a2f7fd548d",
    "balance": 1234,
    "owner": "b4167fba-5ed8-44c0-bc3c-c8dbf94c4092"
}
````

## Update account resource
````
PATCH /api/accounts/<id>
[
	{
    	"op": "replace",
	    "path": "/balance",
    	"value": 55555
	}
]
````
``
Document section to be updated, in order to provide description for other update operations
``

## Delete account resource
````
DELETE /api/accounts/<id>
````