# Grade System Frontend

## Basic

To install dependencies:

```bash
bun install
```

To start a development server:

```bash
bun dev
```

To run for production:

```bash
bun start
```

## Design Phylosophy

1. how to write React componets smoothly: [Thinking in React](https://zh-hans.react.dev/learn/thinking-in-react)

2. how to fetch data from backend: here we take the Tree layers Arch into practice, `client -> modules -> hooks`.

`client`: an util class to tackle actual data sending and recieving, usually get from axios. (`src/client.ts`)

`modules`: claime endpoints used in the project, only defines the interface, contains no logical code. (`src/user.ts`)

`hooks`: abstract the data fetching and updating options from UI componets: componets care nothing about how the data been fetched, they just need to "use". (`src/hooks/*.ts`)

3. how to route between pages: use `wouter` to handle page routing, extremly light-weight and easy to use.
