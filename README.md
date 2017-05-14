# Televic Mechanic Assistant
## Activities
### Overview Of Issues
Consist of a fragment IssueOverviewFragment. This fragment dynamicly handles the REST request and JSONParsing for the list in this fragment. For the REST request Volley is used (see REST Singleton). The ListView needs an adapter to dynamicly load the listitems. Therefore a custom adapter OverviewListAdapter for this view is inherited from the CursorAdapter. We bind the adapter to the database with a CursorLoader. The loader will detect any changes in the database and give the corresponding listitems the dynamicly loaded values.

![Overview Activity](/Git/Screenshots/overview.png?raw=true "The Overview Activity")

### Detailview of an Issue
The data gets loaded from the database to populate the listview. The IMAGE_PRESENT column gets queried to check which IssueAsset have an image attached. If the IssueAsset has an attached image on the server, than a REST-request is handed over to Volley to fetch the image from the server. On response of the server, the bitmap gets saved into the database as a Blob. The CursorLoader notifies the adaptation in the database and populates the images in the listview.

![Detail Activity](/Git/Screenshots/detail.png?raw=true "The Detail Activity")

### Graphs of an Issue
From the Detailview, the user can consult the attached data for the selected Issue. This data is fetched from the server, but is not saved in the database because of its size. Also it is seldom that the user (the mechanic) will need to consult the graphics concerning the Issue.

![Overview Activity](/Git/Screenshots/graphs.png?raw=true "The Graphs")

## Tokens
When the user successfully logs in, the token received from the server is stored in the SharedPreferences. The SharedPreferences are independent of the app's lifecycle, what makes it perfectly suited to store the token in. For every REST request the token is passed in the header. If the token is expired (default 14 days), then a 401 Unauthorized response message is delivered from the server. A 401 results in the user getting redirected to the login activity, where he can log in to receive a new token from the server.

![Overview Activity](/Git/Screenshots/login.png?raw=true "The Overview Activity")

## REST (Volley)
We use a singleton that is created with the Application context. Because it is bound to the application context, it will live for the entire lifetime of the application. Once implemented, this instance makes it very easy to make REST requests and add them to the queue. Volley takes care of the rest.

## Parsing
The REST responses get parsed using JSONObject en JSONArray from the Android API. This is used to extract the data from the JSON-file en insert these values in the database.

## Database
We use a database for caching the requested data. Every app-session, the tables get dropped if they exist. The speed increases fundamentally because we can cache the data through the session. This means that if a user rotates the device, and thus the activity gets destroyed, the data can be loaded directly from the database, without the need for extra network usage to make the same requests again. A refresh option is implemented in the menubar, so that the user can choose to update and sync the viewed list with the data of the server.

## ContentProvider
We query the database using a contentProvider that we can access through the contentResolver in Android. This way, other apps could interact with our app and read or write data from the app's database.

## Work on the app
### Anton Danneels
- REST(Image Requests)
- GraphActivity
- Static version DetailActivity

### Matthias De Lange
- Backend (Database, ContentProvider)
- REST (App Data Requests)
- Parsing (REST data to database)
- Authentication (Tokens, Login)
- OverviewActivity
- Dynamic version DetailActivity
- Documentation
- ReadMe
