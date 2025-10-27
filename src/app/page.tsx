import { redirect } from "next/navigation"

export const metadata = {
  title: "SmartBuoy",
}

export default function Page() {
  redirect("/home")
}
