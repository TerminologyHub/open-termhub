# open-termhub
Open source FHIR® terminology server deployable as a docker container with
local sandbox terminology data provided for testing. This runtime
container works seamlessly with terminology content provided by
[TermHub](https://www.terminologyhub.com) which has a library of code systems, value sets, and concept mappings.

[Video Overview of Open TermHub](https://youtu.be/bHPNpIndyUk)

## Table of Contents

1. [Step-by-step instructions with Sandbox data](doc/TUTORIAL1.md)
2. [Step-by-step instructions with Sandbox data using syndication](doc/TUTORIAL2.md)
3. [Step-by-step instructions to Deploy Terminologies with TermHub](doc/TUTORIAL3.md)

## Other Resources
* [FAQ - Frequently Asked Questions](doc/FAQ.md)
* [More details on building and deploying with real data](doc/DEPLOY.md)
* [Docker image and environment variables](doc/DOCKER.md)

## Configuration highlights

Common environment variables (full list in [DEPLOY.md](doc/DEPLOY.md) and [DOCKER.md](doc/DOCKER.md)):

* `READ_ONLY=true` — rejects HTTP APIs that add, alter, or remove content (403) and disables startup/cron syndication. Read APIs (including FHIR `$` operations and `POST /concept/bulk`) remain available at runtime. Swagger/OpenAPI hides DELETE, PUT, PATCH, and POST operations.
* `ENABLE_POST_LOAD_COMPUTATIONS` — enable tree-position computations used by the hierarchy browser (default: false).
* `PROJECT_API_KEY` / `ADMIN_KEY` — required for TermHub syndication and protected local admin endpoints.
* `PROXY_URL` — optional public origin for Bundle `fullUrl` and next/previous/self when the request has no `X-Forwarded-Host`. Must include a scheme (e.g. `https://fhir.example.org`). `X-Forwarded-Host` / `X-Forwarded-Proto` win when present (including port). Swagger follows those headers or the browser host, not this variable.


## Contributing

1. [Build and Test](doc/BUILD.md)
2. [Docker Image](doc/DOCKER.md)
3. Fork it!
4. Create your feature branch: `git checkout -b my-new-feature`
5. Commit your changes: `git commit -am 'Add some feature'`
6. Push to the branch: `git push origin my-new-feature`
7. Submit a pull request

**[Back to top](#table-of-contents)**

## Current Contributors

- [Brian Carlsen](https://github.com/bcarlsenca)
- [Other Contributors](https://github.com/TerminologyHub/open-termhub/graphs/contributors)

**[Back to top](#table-of-contents)**

## License

See the included [`LICENSE.txt`](LICENSE.txt) file for details.

**[Back to top](#table-of-contents)**