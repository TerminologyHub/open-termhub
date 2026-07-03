# Loading Non-TermHub CodeSystem, ValueSet, or ConceptMap

This documentation describes how to use curl calls to load data from external sources.
We have not tested every possible file from "in the wild" and so there may be issues
that requre attention and interaction with our team at: info@terminologyhub.com.

## CodeSystem

Consider the example code system file below (which the command puts into a CodeSystem.json file). 

```
cat > CodeSystem.json << EOF
{
  "resourceType": "CodeSystem",
  "id": "thisIdWillChangeToAUuidUponLoad",
  "url": "https://example.com/CodeSystem/test",
  "version": "YYYYMMDD",
  "name": "Test Code System",
  "title": "Test code system",
  "status": "active",
  "experimental": false,
  "publisher": "Test Organization",
  "description": "This is a test code system.",
  "caseSensitive": false,
  "content": "complete",
  "property": [
    {
      "code": "inactive",
      "uri": "http://hl7.org/fhir/concept-properties#inactive",
      "description": "True if the concept is not considered active.",
      "type": "boolean"
    }
  ],
  "concept": [
    {
      "code": "C1",
      "display": "Test Code C1",
      "designation": [
        {
          "use": {
            "code": "short"
          },
          "value": "C1"
        }
      ],
      "property": [
        {
          "code": "inactive",
          "valueBoolean": false
        }
      ]
    },
    {
      "code": "C2",
      "display": "Test Code C2",
      "designation": [
        {
          "use": {
            "code": "short"
          },
          "value": "C2"
        }
      ],
      "property": [
        {
          "code": "inactive",
          "valueBoolean": false
        }
      ]
    }
  ]
}
EOF
```

The code system can be loaded by something like the following.

```
curl -X POST 'http://localhost:8080/fhir/CodeSystem/$load' \
  -H 'Content-Type: multipart/form-data' \
  -F 'resource=@CodeSystem.json' | jq
```

Now that the code system is loaded, it can be accessed by URI (FHIR R4 shown below).

```
# Find CodeSystem
curl -s 'http://localhost:8080/fhir/r4/CodeSystem?url=https://example.com/CodeSystem/test' | jq

# Perform a SNOMEDCT CodeSystem $lookup for a code
curl -s 'http://localhost:8080/fhir/r4/CodeSystem/$lookup?system=https://example.com/CodeSystem/test&code=C1' | jq
```

## ValueSet

Consider the example code system file below (which the command puts into a ValueSet.json file). 
It references the code system created above.


```
cat > ValueSet.json << EOF
{
  "resourceType": "ValueSet",
  "id": "thisIdWillChangeToAUuidUponLoad",
  "url": "https://example.com/ValueSet/test",
  "version": "YYYYMMDD",
  "name": "Test Value Set",
  "title": "Test value set",
  "status": "active",
  "experimental": false,
  "publisher": "Test Organization",
  "description": "This is a test value set.",
  "date": "2024-03-01T00:00:00-08:00",
  "compose": {
    "include": [ {
      "system": "https://example.com/CodeSystem/test",
      "concept": [ {
        "code": "C1",
        "display": "C1"
      } ]
    } ]
  }
}
EOF
```

The value set can be loaded by something like the following.

```
curl -X POST 'http://localhost:8080/fhir/ValueSet/$load' \
  -H 'Content-Type: multipart/form-data' \
  -F 'resource=@ValueSet.json' | jq
```

Now that the value set is loaded, it can be accessed by URI (FHIR R4 shown below).

```
# Find ValueSet
curl -s 'http://localhost:8080/fhir/r4/ValueSet?url=https://example.com/ValueSet/test' | jq

# Perform an $expand operation on the ValueSet
curl -s 'http://localhost:8080/fhir/r4/ValueSet/$expand?url=https://example.com/ValueSet/test' | jq
```

## ConceptMap

Consider the example code system file below (which the command puts into a ValueSet.json file). 
It references the code system and value set created above as well as a "test2" set not shown here.

```
cat > ConceptMap.json << EOF
{
  "resourceType": "ConceptMap",
  "id": "thisIdWillChangeToAUuidUponLoad",
  "url": "https://example.com/ConceptMap/test",
  "version": "YYYYMMDD",
  "name": "Test Concept Map",
  "title": "Test concept map",
  "status": "active",
  "experimental": false,
  "publisher": "Test Organization",
  "description": "This is a test concept map.",
  "sourceScopeUri": "https://example.com/ValueSet/test",
  "targetScopeUri": "https://example.com/ValueSet/test2",
  "group": [
    {
      "source": "https://example.com/CodeSystem/test",
      "target": "https://example.com/CodeSystem/test2",
      "element": [
        {
          "code": "C1",
          "display": "D1",
          "target": [
            {
              "code": "X1",
              "relationship": "equivalent"
            }
          ]
        }
      ]
    }
  ]
}
EOF
```

The concept map can be loaded by something like the following.

```
curl -X POST 'http://localhost:8080/fhir/ConceptMap/$load' \
  -H 'Content-Type: multipart/form-data' \
  -F 'resource=@ConceptMap.json' | jq
```

Now that the concept map is loaded, it can be accessed by URI (FHIR R4 shown below).

```
# Find ConceptMap
curl -s 'http://localhost:8080/fhir/r4/ConceptMap?url=https://example.com/ConceptMap/test' | jq

# Perform a ConceptMap $translate to find "target" codes
curl -s 'http://localhost:8080/fhir/r4/ConceptMap/$translate?url=https://example.com/ConceptMap/test&system=https://example.com/CodeSystem/test&code=C1' | jq

curl -s -X DELETE 'http://localhost:8082/fhir/r4/ConceptMap/dc6c2145-7d9f-4e57-be1c-7c5be144c018'
```



