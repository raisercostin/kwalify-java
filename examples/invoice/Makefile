jar = ../../kwalify.jar

all:
	java -classpath $(jar) kwalify.Main -lm invoice.schema.yaml
	java -classpath $(jar) kwalify.Main -lf invoice.schema.yaml invoice.yaml
#	kwalify -lm invoice.schema.yaml
#	kwalify -lf invoice.schema.yaml invoice.yaml

