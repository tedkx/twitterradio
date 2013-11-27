package models;

import scala.Option;
import securesocial.core.AuthenticationMethod;
import securesocial.core.Identity;
import securesocial.core.IdentityId;
import securesocial.core.OAuth1Info;
import securesocial.core.OAuth2Info;
import securesocial.core.PasswordInfo;

public class DebugIdentity implements Identity {
	public DebugIdentity() {  }
	@Override
	public AuthenticationMethod authMethod() { return null; }
	@Override
	public Option<String> avatarUrl() { return null; }
	@Override
	public Option<String> email() { return null; }
	@Override
	public String firstName() { return null; }
	@Override
	public String fullName() { return "Thodwris Kalaitzidis"; }
	@Override
	public String lastName() { return null; }
	@Override
	public Option<OAuth1Info> oAuth1Info() { return null; }
	@Override
	public Option<OAuth2Info> oAuth2Info() { return null; }
	@Override
	public Option<PasswordInfo> passwordInfo() { return null; }
	@Override
	public IdentityId identityId() {
		return new IdentityId("21860462",null);
	}
}