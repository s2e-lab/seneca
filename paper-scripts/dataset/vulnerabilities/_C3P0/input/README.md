# Sample Descripton
"c3p0 is a mature, highly concurrent JDBC Connection pooling library, with support for caching and reuse of PreparedStatements". In the version 0.9.5.2 the API has a class named as `PoolBackedDataSourceBase` that implements the Serializable interface and contains a custom `readObject()` method. This custom method invokes the `IndirectlySerialized->getObject()` if the stream of bytes has an object of type `com.mchange.v2.ser.IndirectlySerialized`.  
The C3P0 library has a class that implements the interface (`com.mchange.v2.naming.ReferenceIndirector$ReferenceSerialized`). 
This class instantiates a class from a remote class path as JNDI ObjectFactory.

## Exploit Example

- Vulnerable version:
	- c3p0-0.9.2.jar

- Fixed Version
	- No fix version available

## External Links

- Github page with latest release
	- https://github.com/swaldman/c3p0

- CVE Details Page for FasterXML jackson-databind library that has a vulnerability due to c3p0
	- https://nvd.nist.gov/vuln/detail/CVE-2018-7489