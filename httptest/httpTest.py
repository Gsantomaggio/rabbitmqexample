__author__ = 'brapigabriele'

import urllib2
import base64
import time
import datetime
import json


def print_time(step):
    ts = time.time();
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S');
    print st + " - " + step;


def call_api(rabbitmqhost, api):
    request = urllib2.Request("http://" + rabbitmqhost + ":15672/api/" + api);
    base64string = base64.encodestring('%s:%s' % ('guest', 'guest')).replace(
        '\n', '')
    request.add_header("Authorization", "Basic %s" % base64string);
    request.get_method = lambda: 'GET';
    response = urllib2.urlopen(request);
    print_time(" *** response done, loading json");
    json.load(response);
    print_time(" *** json loaded");


if __name__ == '__main__':
    RabbitmqHost = "localhost";

    for x in range(0, 1):
        print " --- start test"
        print_time("start get full (old api /queues)");
        call_api(RabbitmqHost, "queues");
        print_time("end get full (old api /queues)");
        print " "

        pages = ["1", "10", "50", "100", "200"];

        for page in pages:
            print_time("start get  (queues?page=" + page + ")");
            call_api(RabbitmqHost, "queues?page=" + page + "&page_size=100");
            print_time("end get  (queues?page=" + page + ")");
            print " "

        print " --- end test"
