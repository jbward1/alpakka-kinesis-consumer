awslocal kinesis put-record \
  --stream-name testing-stream \
  --data eyJpZCI6ImdyaWVqZ2Vyb2lnanJlaWpnIn0= \
  --partition-key `uuidgen`
