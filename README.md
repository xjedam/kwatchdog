kwatchdog
=========

A small application for system monitoring.

## Adding server status examples:
Connect to your mongo database and prepare any date and the server you want to create status for
```
var date = new Date()
var sid = db.servers.find()[0]._id
```
You can save status object as shown below:
```
db.status.save({server_id: sid, date: date, online: false, info: ""})
date = new Date(date.valueOf() - 86400000)
```
