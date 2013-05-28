# sleuth
A very simple applicaiton for logging browser-events.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will also need a MongoDB server, and aws credentials to use DynamoDB.

## Running

To start a web server for the application, run:

    lein run -m sleuth.handler (PORT)

## Usage

To use in a project include the folling in your html:

    <script src="http://ubret.s3.amazonaws.com/sleuth_client.js"></script>

And then initialize somewhere in your javascritp:

    sleuth.init(site_name, site_key)

Sleuth will automatically log any `click` or `change` dom events. If you'd like to a log a different event simply call the following when you'd like to log:

    sleuth.logCustomEvent({type: 'type', value: 0, etc})

`type` and `value` are the only required fields in the object you pass to `sleuth.logCustomEvent` otherwise you can include any other information you'd like to be logged. By default sleuth will automatically include the user id, session id, window width and height, and the user agent string in what it logs to the server. 

## License

Copyright © 2013 Edward Paget

License under the GNU Affero General Public License
