package com.samuelweller.AWS.S3;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.samuelweller.JSONParsing.JSONParser;
import com.samuelweller.Location.KnownLocation;
import com.samuelweller.Location.Location;
import com.samuelweller.LocationService.LL;
import com.samuelweller.config.ApplicationConfig;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class AWSService {

	public String DEFAULT_AWS_BUCKET_NAME;
	public String DEFAULT_AWS_ACCESS_KEY;
	public String DEFAULT_AWS_SECRET_KEY;
	public Region DEFAULT_AWS_REGION;
	
	public AWSService() {
		DEFAULT_AWS_BUCKET_NAME = ApplicationConfig.DEFAULT_AWS_BUCKET_NAME;
		DEFAULT_AWS_ACCESS_KEY = ApplicationConfig.DEFAULT_AWS_ACCESS_KEY;
		DEFAULT_AWS_SECRET_KEY = ApplicationConfig.DEFAULT_AWS_SECRET_KEY;
		DEFAULT_AWS_REGION = ApplicationConfig.DEFAULT_AWS_REGION;
	}

	public void listObjects() {
		
		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();
		
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();
		objects.stream().forEach(System.out::println);
	}
	
	public List<Location> toLocation(List<CSVRecord> list) {

		// Create array to return
		ArrayList<Location> r = new ArrayList<Location>();

		// Fill with list elements
		for (int i = 1; i < list.size(); i++) {
			List<String> row = list.get(i).toList();
			try {
				Location next = new Location(Double.parseDouble(row.get(3).toString()),
						Double.parseDouble(row.get(2).toString()), Long.parseLong(row.get(1).toString()));
				r.add(next);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return r;
	}

	public void TESTcreateObject() {
		
		System.out.println("Parsing ... ");

//		// Read data from csv
//		List<Location> data = null;
//		try {
//			BufferedReader csvReader = new BufferedReader(new InputStreamReader(
//					AWSService.class.getClassLoader().getResource("static/new_cleaned2.csv").openStream()));
//			CSVParser parser = new CSVParser(csvReader, CSVFormat.RFC4180);
//			data = this.toLocation(parser.getRecords());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		String json = null;
//		try {
//			json = Files.readString(Path.of(this.getClass().getResource("/static/locations.json").toURI()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		long currentTimeMillis = System.currentTimeMillis();
//		List<Location> parse = JSONParser.parse(json);
//		System.out.println(parse.size());
//		System.out.println("Time taken to parse alone: " + (System.currentTimeMillis() - currentTimeMillis)/1000 + "s");

		
		// Create Object
//		this.createLocationsIfCan("sweller", parse);
	}

	private void createLocationsIfCan(String user, List<Location> locations) {

		// Check if can create
		if (this.doesBucketExist(DEFAULT_AWS_BUCKET_NAME)) {
			if (!this.doesObjectExist(user)) {
				// Create object
				this.createLocations(user, locations);
			}
		}
	}
	
	public void deleteIfExists(String objectName) {
		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Ensure object exists
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();
		if (objects.stream().filter(object -> object.key().equals(objectName)).count() > 0) {
			
			// Build object to delete
			ArrayList<ObjectIdentifier> toDelete = new ArrayList<ObjectIdentifier>();
	        toDelete.add(ObjectIdentifier.builder().key(objectName).build());
			
			// Delete object
			 DeleteObjectsRequest dor = DeleteObjectsRequest.builder()
	                    .bucket(DEFAULT_AWS_BUCKET_NAME)
	                    .delete(Delete.builder().objects(toDelete).build())
	                    .build();
	            s3.deleteObjects(dor);
		}
	}
	
	public void createKnownLocationsIfCan(String user, List<KnownLocation> knownLocations) {

		String userFinal = user + " - known locations";
		
		// Check if can create
		if (this.doesBucketExist(DEFAULT_AWS_BUCKET_NAME)) {
			if (!this.doesObjectExist(userFinal)) {
				// Create object
				this.createKnownLocations(userFinal, knownLocations);
			}
		}
	}
	
	private void createKnownLocations(String userFinal, List<KnownLocation> knownLocations) {

		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Ensure object doesn't exists
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();
		if (objects.stream().filter(object -> object.key().equals(userFinal)).count() > 0) {
			throw new Error("Object - " + userFinal + " - in bucket - " + DEFAULT_AWS_BUCKET_NAME + " - already exists");
		}

		// Create bytes from location data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(bos);
			os.writeObject(knownLocations);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create object
		s3.putObject(PutObjectRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).key(userFinal).build(),
				RequestBody.fromBytes(bos.toByteArray()));

	}

	public boolean doesObjectExist(String user) {

		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Object request
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();

		return objects.stream().filter(object -> object.key().equals(user)).count() > 0 ? true : false;
	}

	public boolean doesBucketExist(String... _bucketName) {

		// Optional parameters - Bucket Name
		String bucketName = _bucketName.length > 0 ? _bucketName[0] : DEFAULT_AWS_BUCKET_NAME;

		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Bucket request
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();

		// Return
		return buckets.stream().filter(bucket -> bucket.name().equals(bucketName)).count() == 0 ? false : true;
	}

	// Would usually pass in List<Location> to write
	public void createLocations(String user, List<Location> locations) {

		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Ensure object doesn't exists
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();
		if (objects.stream().filter(object -> object.key().equals(user)).count() > 0) {
			throw new Error("Object - " + user + " - in bucket - " + DEFAULT_AWS_BUCKET_NAME + " - already exists");
		}

		// Create bytes from location data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(bos);
			os.writeObject(locations);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create object
		s3.putObject(PutObjectRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).key(user).build(),
				RequestBody.fromBytes(bos.toByteArray()));

	}

	@Cacheable(value = "locations")
	public LL getLocations(String user) {

		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Ensure object exists
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();

		if (objects.stream().filter(object -> object.key().equals(user)).count() == 0) {
			throw new Error("There is no object - " + user + " - in bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Retrieve object
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).key(user).build();
		ResponseInputStream<GetObjectResponse> object = s3.getObject(getObjectRequest);
		ByteArrayInputStream bis = null;

		// Possible IO exception when reading bytes
		try {
			bis = new ByteArrayInputStream(object.readAllBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Parse bytes to desired object
		ObjectInput in = null;
		List<Location> out = null;
		try {
			in = new ObjectInputStream(bis);
			out = (List<Location>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Best practise to close stream?
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		System.out.println("Returning from AWS because - " + user);

		return new LL(out);
	}

	@Cacheable(value = "knownLocations")
	public List<KnownLocation> getKnownLocations(String user) {
		
		final String userFinal = user + " - known locations";
		
		// Credentials
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(DEFAULT_AWS_ACCESS_KEY, DEFAULT_AWS_SECRET_KEY);
		S3Client s3 = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.region(DEFAULT_AWS_REGION).build();

		// Ensure bucket exists
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<Bucket> buckets = listBucketsResponse.buckets();
		if (buckets.stream().filter(bucket -> bucket.name().equals(DEFAULT_AWS_BUCKET_NAME)).count() == 0) {
			throw new Error("There is no bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Ensure object exists
		ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).build();
		ListObjectsResponse res = s3.listObjects(listObjects);
		List<S3Object> objects = res.contents();

		if (objects.stream().filter(object -> object.key().equals(userFinal)).count() == 0) {
			throw new Error("There is no object - " + userFinal + " - in bucket - " + DEFAULT_AWS_BUCKET_NAME);
		}

		// Retrieve object
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(DEFAULT_AWS_BUCKET_NAME).key(userFinal).build();
		ResponseInputStream<GetObjectResponse> object = s3.getObject(getObjectRequest);
		ByteArrayInputStream bis = null;

		// Possible IO exception when reading bytes
		try {
			bis = new ByteArrayInputStream(object.readAllBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Parse bytes to desired object
		ObjectInput in = null;
		List<KnownLocation> out = null;
		try {
			in = new ObjectInputStream(bis);
			out = (List<KnownLocation>) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Best practise to close stream?
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				// ignore close exception
			}
		}

		System.out.println("Returning from AWS because - " + userFinal);

		return out;
		
	}

	@CacheEvict(value = "knownLocations", key = "#user")
	public void addKnownLocation(String user, KnownLocation knownLocation) {
		List<KnownLocation> knownLocations = this.getKnownLocations(user);
		knownLocations.add(knownLocation);
		this.deleteIfExists(user + " - known locations");
		this.createKnownLocationsIfCan(user, knownLocations);
	}
	
	@CacheEvict(value = "knownLocations", key = "#user")
	public void removeKnownLocation(String user, KnownLocation knownLocation) {
		List<KnownLocation> knownLocations = this.getKnownLocations(user);
		System.out.println(knownLocations);
		System.out.println("Location to remove: " + knownLocation);
		List<KnownLocation> newKnownLocations = knownLocations.stream()
				.filter(loc -> !loc.getName().equals(knownLocation.getName()))
				.collect(Collectors.toList());
		System.out.println(newKnownLocations);
		this.deleteIfExists(user + " - known locations");
		this.createKnownLocationsIfCan(user, newKnownLocations);
	}
}
