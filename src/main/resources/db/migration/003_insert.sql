update flow set validation = '{
  "fieldType": "DATE",
  "dateValidationConfig": {
    "gte": "2022-09-10",
    "lte": "now",
    "format": "dd-MM-yyyy"
  }
}'  where id ='258c2050-135f-4e3f-8422-9b28cb975176';

update flow set validation = '{
  "fieldType": "TEXT",
  "textValidationConfig": {
    "max": "100",
    "min": "10",
    "regex": "^[a-zA-Z0-9! ]*$"
  }
}'  where id ='9a53b60e-3ecc-4e2b-a175-9d657c52809f';

update flow set validation = '{
  "fieldType": "TEXT",
  "textValidationConfig": {
    "length": "10"
  }
}'  where id ='c0cfb624-4a0a-48f1-822e-95a9c949041b';

update flow set validation = '{
  "fieldType": "TEXT",
  "textValidationConfig": {
    "regex": "[A-Z0-9]",
    "length": "10"
  }
}' where id ='d1ec737b-a650-44b7-a106-0ec7f80e6a30';

update flow set validation = '{
  "fieldType": "DATE",
  "dateValidationConfig": {
    "gte": "2022-09-10",
    "lte": "now",
    "format": "dd-MM-yyyy"
  }
}' where id ='fce2c19d-315f-4723-95eb-2f6e8850dac2';
