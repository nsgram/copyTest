curl --location --request POST 'https://sit1-api.cvshealth.com/file/scan/v1/upload' \
--header 'x-api-key: T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w' \
--header 'Content-Type: application/json' \
--data-raw '{
    "file":"JVBERi0xLjMKJbrfrOAKMyAwIG9iago8PC9",
    "state":"teststate",
    "fileName":"screenshot"
}'
