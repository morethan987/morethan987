import { LoginPage, SignupPage, NotFoundPage, Dashboard } from "./pages";
import { Route, Switch, Redirect } from "wouter";
import { ROUTES } from "./routes";
import { ThemeProvider } from "@/components/theme-provider";
import { AuthProvider, ProtectedRoute } from "@/contexts/auth-context";
import "styles/globals.css";

export function App() {
  return (
    <ThemeProvider
      attribute="class"
      defaultTheme="system"
      enableSystem
      disableTransitionOnChange
    >
      <AuthProvider>
        <Switch>
          <Route path="/">
            <Redirect to={ROUTES.LOGIN} />
          </Route>

          <Route path={ROUTES.LOGIN} component={LoginPage} />

          <Route path={ROUTES.SIGNUP} component={SignupPage} />

          <Route path={ROUTES.FORGOT_PASSWORD}>
            <div>Forgot Password Page - To be implemented</div>
          </Route>

          <Route path={ROUTES.DASHBOARD}>
            <ProtectedRoute fallback={<Redirect to={ROUTES.LOGIN} />}>
              <Dashboard />
            </ProtectedRoute>
          </Route>

          <Route component={NotFoundPage}></Route>
        </Switch>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
