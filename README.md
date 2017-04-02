# bachelorproject_app

## Overview Activity
Consist of a fragment IssueOverviewFragment. This fragment dynamicly handles the REST request and JSONParsing for the list in this fragment. For the REST request Volley is used (see REST Singleton). The ListView needs an adapter to dynamicly load the listitems. Therefore a custom adapter OverviewListAdapter for this view is inherited from the BaseAdapter. We bind the adapter to the fragment by passing the context of the fragment. Thereafter we can initialize the LayoutInflater to access the layout items in the list, and give them the dynamicly loaded values.

TODO: 
- On view rotation: AsyncTask must not refresh
- Make real connection with server (delete testString for REST)

Note: Now the list loads rather slow, because it waits on timeout-error for the testString of the REST.

## REST Singleton
We use a singleton that is created with the Application context. Because it is bound to the application context, it will live for the entire lifetime of the application. Once implemented, this instance makes it very easy to make REST requests and add them to the queue. Volley takes care of the rest.
