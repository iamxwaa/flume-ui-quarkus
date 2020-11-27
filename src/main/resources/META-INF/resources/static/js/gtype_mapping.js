var channels_mapping = {
    "file": "file-channel",
    "jdbc": "jdbc-channel",
    "org.apache.flume.channel.kafka.KafkaChannel": "kafka-channel",
    "jdorg.apache.flume.channel.PseudoTxnMemoryChannelbc": "pseudo-transaction-channel",
    "SPILLABLEMEMORY": "spillable-memory-channel",
    "memory": "memory-channel"
}

var sources_mapping = {
    "avro": "avro-source",
    "exec": "exec-source",
    "http": "http-source",
    "jms": "jms-source",
    "org.apache.flume.source.kafka.KafkaSource": "kafka-source",
    "netcat": "netcat-tcp-source",

    "netcatudp": "netcat-udp-source",
    "seq": "sequence-generator-source",
    "spooldir": "spooling-directory-source",
    "org.apache.flume.source.StressSource": "stress-source",
    "TAILDIR": "taildir-source",
    "thrift": "thrift-source",

    "com.vrv.vap.zipsource.ZipPictureSource": "vap-client-zip1-source",
    "com.vrv.vap.zipsource.ZipSource": "vap-client-zip-source",
    "com.vrv.vap.dbsource.DatabaseSourceStmt": "vap-database1-source",
    "com.vrv.vap.dbsource.DatabaseSource": "vap-database-source",
    "com.vrv.vap.dysource.TcpServerSource": "vap-dynamic1-source",
    "com.vrv.vap.dysource.UdpServerSource": "vap-dynamic2-source",

    "com.vrv.vap.dysource.SyslogServerSource": "vap-dynamic3-source",
    "com.vrv.vap.dysource.Nt4TcpServerSource": "vap-dynamic4-source",
    "com.vrv.vap.dysource.Nt4UdpServerSource": "vap-dynamic5-source",
    "com.vrv.vap.dysource.UdpAssetsServerSource": "vap-dynamic6-source",
    "com.vrv.vap.dysource.FileSource": "vap-dynamic-source",
    "com.vrv.vap.essource.ElasticSearchHuaweiSource": "vap-elasticsearch1-source",

    "com.vrv.vap.essource.ElasticSearchScrollSource": "vap-elasticsearch2-source",
    "com.vrv.vap.essource.ElasticSearchSource": "vap-elasticsearch-source",
    "com.vrv.vap.kafkasource.KafkaSource": "vap-kafka-source",
    "com.vrv.vap.redissource.RedisSource": "vap-redis-source",
    "com.vrv.vap.sftpsource.SFTPSource": "vap-sftp-source",
    "com.vrv.vap.snmpsource.SNMPSource": "vap-snmp-source",

    "com.vrv.vap.spec.lvmengsource.LvMengSource": "vap-spec-lvmeng-source",
    "com.vrv.vap.spec.twassource.TwasSource": "vap-spec-twas-source",
    "com.vrv.vap.wssource.VrvInnerNetWsSource": "vap-webservice1-source",
    "com.vrv.vap.wssource.DemoWsSource": "vap-webservice-source"
}

var sinks_mapping = {
    "avro": "avro-sink",
    "org.apache.flume.sink.elasticsearch.ElasticSearchSink": "elasticsearchsink",
    "file_roll": "file-roll-sink",
    "hdfs": "hdfs-sink",
    "hive": "hive-sink",
    "http": "http-sink",

    "irc": "irc-sink",
    "org.apache.flume.sink.kafka.KafkaSink": "kafka-sink",
    "org.apache.flume.sink.kite.DatasetSink": "kite-dataset-sink",
    "logger": "logger-sink",
    "org.apache.flume.sink.solr.morphline.MorphlineSolrSink": "morphlinesolrsink",
    "null": "null-sink",

    "thrift": "thrift-sink",
    "com.vrv.vap.complex.pic.EsAndHbaseSink": "vap-complex-sink",
    "com.vrv.vap.dbsink.DatabaseSink": "vap-database-sink",
    "com.vrv.vap.datahubsink.DataHubSink": "vap-datahub-sink",
    "com.vrv.vap.essink.ElasticSearchHuaweiSink": "vap-elasticsearch1-sink",
    "com.vrv.vap.essink.ElasticSearchSink": "vap-elasticsearch-sink",

    "com.vrv.vap.hbasesink.HbaseZipPictureSink": "vap-hbase-sink",
    "com.vrv.vap.hivesink.HiveTextSink": "vap-hive1-sink",
    "com.vrv.vap.hivesink.HiveParquetSink": "vap-hive-sink",
    "com.vrv.vap.kafkasink.KafkaSink": "vap-kafka-sink",
    "com.vrv.vap.vrvlog.PictureSink": "vap-log1-sink",
    "com.vrv.vap.vrvlog.DataFixLogSink": "vap-log3-sink",

    "com.vrv.vap.vrvlog.JsonStringSink": "vap-log-sink",
    "com.vrv.vap.socketsink.TcpSink": "vap-socket1-sink",
    "com.vrv.vap.socketsink.SyslogSink": "vap-socket2-sink",
    "com.vrv.vap.socketsink.UdpSink": "vap-socket-sink"
}

var interceptors_mapping = {
    "com.vrv.vap.interceptor.FunctionInterceptor$Builder": "vap-function-interceptor",
    "com.vrv.vap.interceptor.AvroInterceptor$Builder": "vap-avro-interceptor"
}