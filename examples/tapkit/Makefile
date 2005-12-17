jar = ../../kwalify.jar

all:
	java -classpath $(jar) kwalify.Main -lm tapkit.schema.yaml
	java -classpath $(jar) kwalify.Main -lf tapkit.schema.yaml tapkit.yaml
#	kwalify -lm tapkit.schema.yaml
#	kwalify -lf tapkit.schema.yaml tapkit.yaml

