import boto3

client = boto3.client(
    'dynamodbstreams',
    region_name='us-east-1',
    aws_access_key_id='test',
    aws_secret_access_key='test',
    endpoint_url='http://localhost:4566'  # 👈 este es el endpoint de LocalStack
)

stream_arn = "arn:aws:dynamodb:us-east-1:000000000000:table/InfoRequest/stream/2025-11-09T21:04:36.700"
response = client.describe_stream(StreamArn=stream_arn)
shard_id = response['StreamDescription']['Shards'][0]['ShardId']

iterator = client.get_shard_iterator(
    StreamArn=stream_arn,
    ShardId=shard_id,
    ShardIteratorType='TRIM_HORIZON'
)['ShardIterator']

records = client.get_records(ShardIterator=iterator)
for record in records['Records']:
    print(record)