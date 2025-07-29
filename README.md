## Rate Limiter

This is a simple rate limiter implementation which mimics limiting request for same account to same URL within certain amount of time. First, you need to set MAX_CONNECTION and TIME_WINDOW_IN_MILLIS using CLI args, otherwise it will be set to default. Then you can start mimicking request for each account and URL.

## Allowed Commands

```
init <time_window_in_millis> <max_request>
```

`time_window_in_millis` and `max_request` are integer.

```
request <account> <request_url>
```
`account` and `request_url` are string.