# bowling-scorecard

A Clojure API designed to simulate a bowling scorecard

## Usage

This API is built and tested with lein, other clojure engines could also work, but are not guaranteed to do so.
To run the API at port 3000, or next available:

    lein ring server

To create a new scorecard send a POST request to 

    localhost:<PORT>/v1/scorecard

This will return a scorecard id.

To view all scorecard ids currently in memory, send a GET request to:

    localhost:<PORT>/v1/scorecard

To add a frame to the next available slot in a scorecard, send a POST request to:

    localhost:<PORT>/v1/scorecard/<scorecard-id>

Also include the x-www-form-urlencoded params:
    first <int>
    second <int> optional
    third <int> optional
These params should refer to the number of pins knocked down with each individual bowl, not sums on the second or third.

To view a current board, send a GET request to:

    localhost:<PORT>/v1/scorecard/<scorecard-id>

Happy bowling!

## License

Copyright © 2019 Djole Minic
