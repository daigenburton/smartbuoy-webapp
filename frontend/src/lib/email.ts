// lib/email.ts
import { Resend } from "resend";

const resend = new Resend(process.env.RESEND_API_KEY);

export async function sendNotificationEmail(to: string, message: string) {
  await resend.emails.send({
    from: "SmartBuoy <alerts@smartbuoy.us>",
    to,
    subject: "SmartBuoy Alert",
    html: `
      <p>${message}</p>
      <p>Check your dashboard for more details.</p>
    `,
  });
}