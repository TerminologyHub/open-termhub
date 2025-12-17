# Frequently Asked Questions

### General Questions

* **What is Open TermHub?**

> An open-source FHIR API-based service for accessing standard healthcare terminologies relevant for clinical and research use cases.  It is designed to work seamlessly with https://terminologyhub.com.

[Back to Top](#frequently-asked-questions)

### Questions about Mappings

* **Is there a way to bulk search mappings?**

> There is not a specific API for bulk searching mappings, but the *native API* call for finding mappings within a mapset can be used for this purpose.  For example, the following call finds all mappings with a *from code* of either `75934005` or `126926005` in the mapset with abbreviation `SNOMEDCT_US-ICD10CM`:
>
>```
>curl -G 'https://opentermhub.terminology.tools/mapset/SNOMEDCT_US-ICD10CM/mapping' \
>    --data-urlencode "query=from.code:(75934005 OR 126926005)" 
>```
>
> <p>For more than 2 codes, just join them all together with " OR " within the parentheses of the above query.  There is an ultimate limit to query string size in URLs and so this will not scale to thousands of codes, so you will need to break such a scenario up into multiple calls.</p>


[Back to Top](#frequently-asked-questions)
