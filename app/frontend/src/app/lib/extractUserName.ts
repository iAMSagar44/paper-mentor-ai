import base64 from 'base-64';

interface Claim {
    typ: string;
    val: string;
  }

export function extractUserName(clientPrincipal: string) {
    const decoded = base64.decode(clientPrincipal);
    const token = JSON.parse(decoded);
    //console.log("The decoded token is ", token);
    const claims = token.claims.reduce((acc: { [key: string]: string }, claim: Claim) => {
        acc[claim.typ] = claim.val;
        return acc;
      }, {});
      //console.log("The claims are ", claims);
    return claims;
}