import hashlib
m = hashlib.md5()
m.update("8abd483b97b2c74bbd9")
#m.update("1f5c465f2aebc8090cb") #ye
#m.update("6985a494e6fdf438347")
#m.update("2564aa958bd681bae07") # htz
#m.update("e90ef382d30496c6cb5") # nlb
#m.update("42cf145493b4acd1679") # ye2
#m.update("1c7ceb2717942afab74") #zfy
#m.update("01fe7d50598d5105d03") #zfy2
m.update("LuCkYm0nEyKeY")
print m.hexdigest()[4:17]
