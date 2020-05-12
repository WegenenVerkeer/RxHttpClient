package be.wegenenverkeer.rxhttpclient;

/**
 * HTTP status codes as documented in RFC 2616
 *
 * Created by Karel Maesen, Geovise BVBA on 19/12/14.
 */
//TODO -- this can be deprecated and replaced by org.asynchttpclient.util.HttpConstants.ResponseStatusCodes
public class HTTPStatusCode {

    final static  public int  SwitchingProtocols = 101;
    final static  public int  OK = 200;
    final static  public int  Created = 201;
    final static  public int  Accepted = 202;
    final static  public int  NonAuthoritativeInformation = 203;
    final static  public int  NoContent = 204;
    final static  public int  ResetContent = 205;
    final static  public int  PartialContent = 206;
    final static  public int  MultipleChoices = 300;
    final static  public int  MovedPermanently = 301;
    final static  public int  Found = 302;
    final static  public int  SeeOther = 303;
    final static  public int  NotModified = 304;
    final static  public int  UseProxy = 305;
    final static  public int  TemporaryRedirect = 307;
    final static  public int  BadRequest = 400;
    final static  public int  Unauthorized = 401;
    final static  public int  PaymentRequired = 402;
    final static  public int  Forbidden = 403;
    final static  public int  NotFound = 404;
    final static  public int  MethodNotAllowed = 405;
    final static  public int  NotAcceptable = 406;
    final static  public int  ProxyAuthenticationRequired = 407;
    final static  public int  RequestTimeout = 408;
    final static  public int  Conflict = 409;
    final static  public int  Gone = 410;
    final static  public int  LengthRequired = 411;
    final static  public int  PreconditionFailed = 412;
    final static  public int  RequestEntityTooLarge = 413;
    final static  public int  RequestURITooLarge = 414;
    final static  public int  UnsupportedMediaType = 415;
    final static  public int  RequestedRangeNotSatisfiable = 416;
    final static  public int  ExpectationFailed = 417;
    final static  public int  InternalServerError = 500;
    final static  public int  NotImplemented = 501;
    final static  public int  BadGateway = 502;
    final static  public int  ServiceUnavailable = 503;
    final static  public int  GatewayTimeOut = 504;
    final static  public int  HTTPVersionNotSupported = 505;

}
